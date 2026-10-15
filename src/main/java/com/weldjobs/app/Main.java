package com.weldjobs.app;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String menu = """
                === WELDJOBS CONSOLE ===
                1. Thợ hàn
                2. Tin tuyển dụng
                3. Thoát
                Chọn chức năng:
                """;
        Scanner scanner = new Scanner(System.in);
        System.out.print(menu);

        String input = scanner.nextLine();
        String message = switch (input){
            case "1" ->"Thợ hàn";
            case "2" -> "Tin tuyển dụng";
            case "3" -> "Thoát";
            default -> "Lựa chọn không hợp lệ";
        };
        System.out.println(message);
        scanner.close();
    }

}