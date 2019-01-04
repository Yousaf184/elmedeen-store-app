<?php

header('Content-Type: application/json');

include_once 'DbConnection.php';

$dbConnection = DbConnection::getConnection();

$queryParam = null;
$sql = null;

if($_GET['app_name']) {
    $queryParam = $_GET['app_name'];
}

if($queryParam !== null) {
    $sql = 'SELECT * FROM storeapps WHERE app_name_eng = ?';
} else {
    $sql = 'SELECT * FROM storeapps';
}

$stmt = $dbConnection->prepare($sql);

if($queryParam !== null) {
    $stmt->bind_param('s', $queryParam);
}

$stmt->execute();

$resultSet = $stmt->get_result();

if($resultSet->num_rows > 0) {
    $response = array();

    while ($row = $resultSet->fetch_assoc()) {
        $rowData = (Object) array(
            'app_name_eng' => $row['app_name_eng'],
            'app_name_urd' => $row['app_name_urd'],
            'app_name_arb' => $row['app_name_arb'],
            'app_description_eng' => $row['app_description_eng'],
            'app_description_urd' => $row['app_description_urd'],
            'app_description_arb' => $row['app_description_arb'],
            'app_apk_size' => $row['app_apk_size'],
            'app_package_name' => $row['app_package_name'],
            'app_title_image_url' => $row['app_title_image_url'],
            'app_version_url' => $row['app_version_url'],
            'app_download_url' => $row['app_download_url'],
            'app_screenshot1_url' => $row['app_screenshot1_url'],
            'app_screenshot2_url' => $row['app_screenshot2_url']
        );

        array_push($response, $rowData);
    }

    $data = (Object) array(
        'status' => 'success',
        'apps' => $response
    );

    echo json_encode($data);

} else {
    $res = (Object) array(
        'status' => 'error',
        'error_message' => 'App not found'
    );

    echo json_encode($res);
}

$stmt->close();
$dbConnection->close();
