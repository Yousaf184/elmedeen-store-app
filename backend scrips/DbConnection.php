<?php

class DbConnection
{
    private static $server = 'my02.winhost.com';
    private static $username = 'mobile';
    private static $password = 'Mobile@123';
    private static $database = 'mysql_78267_mobile';

    public static function getConnection() {
        $conn = new mysqli(
                            DbConnection::$server,
                            DbConnection::$username,
                            DbConnection::$password,
                            DbConnection::$database
                           );
        return $conn;
    }
}