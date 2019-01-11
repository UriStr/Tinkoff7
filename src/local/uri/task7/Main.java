package local.uri.task7;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        //Переменная количества отчетов (см. класс Report) и ссылка на список, содержащий эти отчеты
        int setDimension;
        List<Report> reportList = new LinkedList<>();


        //Инициализируем переменную количества отчетов и заполняем список отчетов
        try {
            Scanner scanner = new Scanner(new File("test.txt"));
            scanner.useDelimiter(System.getProperty("line.separator"));

            setDimension = Integer.parseInt(scanner.nextLine());

            while (scanner.hasNextLine()) {
                String[] s = new String[3];

                for (int i = 0; i < 3; i++) {
                    s[i] = scanner.nextLine();
                }

                Report report = new Report(s[0], s[1], Integer.parseInt(s[2]));
                reportList.add(report);
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        //Сортируем список с отчетами по дате сдачи в порядке увеличения
        reportList.sort((Report x, Report y) -> {
            if (x.getReportPassDate().isBefore(y.getReportPassDate())) {
                return -1;
            } else if (x.getReportPassDate().isAfter(y.getReportPassDate())) {
                return 1;
            } else {
                return 0;
            }
        });


        /*Суть решения: будем пробовать "подготовить" все отчеты, начиная с самой поздней возможной даты начала
        подготовки всех отчетов(за сутки до сдачи самого срочного отчета) и, если начиная с такой даты не сможем
        подготовить все отчеты вовремя, будем двигаться к самому раннему дню (за Tk дней до сдачи самого срочного
        отчета)*/

        //Получаем самый срочный отчет (он первый в отсортированном списке reportList)
        Report firstReport = ((LinkedList<Report>) reportList).getFirst();

        //Определяем и инициализируем самую раннюю и самую позднюю даты начала подготовки всех отчетов
        LocalDate earliestDate = firstReport.getReportPassDate().minusDays(firstReport.getReportPrepDays());
        LocalDate lastPossibleDay = earliestDate.plusDays(firstReport.getReportPrepDays() - 1);

        /*Выполняем попытки со всеми возможными датами начала выполнения всех отчетов, начиная самой поздней,
        до тех пор, пока в какую-либо из дат не удастся выполнить все отчеты в срок. В противном случае будет
        в консоль выведено "NO"*/
        while (lastPossibleDay.isAfter(earliestDate) || lastPossibleDay.isEqual(earliestDate)) {

            /*Инициализируем текущую дату (дату, с которой начинаем прогон). Внимание, это не присвоение ссылки на
            объект LocalDate, а создание такого же объекта, хз почему так, но работает как clone() для классов
            реализующих интерфейс Clonеable*/
            LocalDate currentDay = lastPossibleDay;

            //Пытаемся выполнять
            while ((notAllArePrepared(reportList) && everyNotPreparedAreAbleToBePrepared(reportList)) || (currentDay.isBefore(((LinkedList<Report>) reportList).getLast().getReportPassDate()))) {

                //Флаг сигнализирует о том, был ли выполнен хотя бы один отчет за текущий день
                boolean flag = false;

                for (Report report : reportList
                ) {
                    //Если был выполнен хотя бы один отчет - другие отчеты уже не могут быть выполнены в этот же день
                    if (flag) break;

                    //Если отчет не готов
                    if (!report.isPrepared()) {
                        //Возможно ли его подготовить в дальнейшем (в зависимости от текущей даты)?
                        report.setPossibleToPrepare(isPossibleToBePrepared(report, currentDay));
                        //Проставить ему статус "подготовлен", если это возможно
                        setReportPrepared(report, currentDay, reportList);
                        if (report.isPrepared) {
                            flag = true;
                        }
                    }
                }

                //Текущая дата сменяется следующей календарной
                currentDay = currentDay.plusDays(1);
            }

            /*Если начиная с последнего позможного дня начала подготовки всех отчетов мы смогли подготовить их все, то
            выводим дату этого дня начала*/
            if (allArePrepared(reportList)) {
                System.out.println(lastPossibleDay.toString());
                System.exit(0);
            }

            //Если не смогли, то пробуем начать с другого (предшествующего) дня
            lastPossibleDay = lastPossibleDay.minusDays(1);
        }

        //Если, начав в каждый из возможных дней начала, не смогли подготовить все отчеты - выводим "NO"
        System.out.println("NO");
    }

    //Есть ли в списке еще невыполненные отчеты?
    private static boolean notAllArePrepared(List<Report> reportList) {
        for (Report r : reportList
        ) {
            if (!r.isPrepared()) {
                return true;
            }
        }
        return false;
    }

    //Все ли отчеты среди неподготовленных можно выполнить в дальнейшем?
    private static boolean everyNotPreparedAreAbleToBePrepared(List<Report> reportList) {

        for (Report r : reportList
        ) {
            if (!r.isPossibleToPrepare()) {
                return false;
            }
        }
        return true;
    }

    //Все ли отчеты в списке подготовлены?
    private static boolean allArePrepared(List<Report> reportList) {
        for (Report r : reportList
        ) {
            if (!r.isPrepared()) {
                return false;
            }
        }
        return true;
    }

    //Можно ли будет выполнить этот отчет в дальнейшем?
    private static boolean isPossibleToBePrepared(Report report, LocalDate currentDate) {
        if (currentDate.isBefore(report.getReportPassDate())) {
            return true;
        }
        return false;
    }

    /*Поставить отчету статус "Подготовлен", если до этого не было подготовленных отчетов, если в текущий день можно
    начинать выполнять данный очтет, если уже не поздно выполнять этот отчет*/
    private static void setReportPrepared(Report report, LocalDate currentDate, List<Report> reportList) {

        for (Report r : reportList
        ) {
            if (r.getReportPassDate().isEqual(currentDate)) {
                return;
            }
        }

        if (currentDate.isBefore(report.getReportPassDate()) & currentDate.isAfter(report.getReportPassDate().minusDays(report.getReportPrepDays() + 1))) {
            report.setPrepared(true);
        }
    }

    //Класс единичного отчета
    static class Report {

        private String reportName;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        //Дата сдачи отчета
        private LocalDate reportPassDate;

        //Число дней, раньше которых нельзя начинать готовить отчет
        private int reportPrepDays;

        //Готов ли отчет?
        boolean isPrepared = false;

        //Будет ли еще возможность подготовить отчет в дальнейшем? (зависит от текущей даты)
        boolean isPossibleToPrepare = true;


        public Report(String reportName, String reportPassDate, int reportPrepDays) {
            this.reportName = reportName;
            LocalDate localDate = LocalDate.parse(reportPassDate, dateTimeFormatter);
            this.reportPassDate = localDate;
            this.reportPrepDays = reportPrepDays;
        }

        public String getReportName() {
            return reportName;
        }

        public void setReportName(String reportName) {
            this.reportName = reportName;
        }

        public LocalDate getReportPassDate() {
            return reportPassDate;
        }

        public void setReportPassDate(LocalDate reportPassDate) {
            this.reportPassDate = reportPassDate;
        }

        public int getReportPrepDays() {
            return reportPrepDays;
        }

        public void setReportPrepDays(int reportPrepDays) {
            this.reportPrepDays = reportPrepDays;
        }

        public boolean isPrepared() {
            return isPrepared;
        }

        public void setPrepared(boolean prepared) {
            isPrepared = prepared;
        }

        public boolean isPossibleToPrepare() {
            return isPossibleToPrepare;
        }

        public void setPossibleToPrepare(boolean possibleToPrepare) {
            isPossibleToPrepare = possibleToPrepare;
        }
    }
}