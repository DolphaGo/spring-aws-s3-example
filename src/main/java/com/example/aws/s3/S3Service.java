package com.example.aws.s3;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor
public class S3Service {
    private AmazonS3 s3Client;
    // lombok 패키지가 아닌, org.springframework.beans.factory.annotation 패키지임에 유의합니다.
    // 해당 값은 application.yml에서 작성한 cloud.aws.credentials.accessKey 값을 가져옵니다.
    @Value("${aws.credentials.accessKey}")
    private String accessKey;
    @Value("${aws.credentials.secretKey}")
    private String secretKey;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.region.static}")
    private String region;

    @PostConstruct // 의존성 주입이 이루어진 후 초기화를 수행하는 메서드이며, bean이 한 번만 초기화 될 수 있도록 해줍니다.
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey); // accessKey와 secretKey를 이용하여 자격증명 객체를 얻습니다.
        // AmazonS3Client가 deprecated됨에 따라, AmazonS3ClientBuilder를 사용했습니다.
        // 이렇게 해주는 목적은 AmazonS3ClientBuilder를 통해 S3 Client를 가져와야 하는데, 자격증명을 해줘야 S3 Client를 가져올 수 있기 때문입니다.
        s3Client = AmazonS3ClientBuilder.standard()
                                        .withCredentials(new AWSStaticCredentialsProvider(credentials)) // 자격증명을 통해 S3 Client를 가져옵니다.
                                        .withRegion(this.region) // 예제에서는 application.yml에 있는 값을 설정 했는데, Regions enum을 통해 설정할수도 있습니다.
                                        .build();
        // 자격증명이란 accessKey, secretKey를 의미하는데,
        // 의존성 주입 시점에는 @Value 어노테이션의 값이 설정되지 않아서 @PostConstruct를 사용했습니다.
    }

    public String upload(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();

        s3Client.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), null) // 업로드를 하기 위해 사용되는 함수입니다
                                   .withCannedAcl(CannedAccessControlList.PublicRead)); // 외부에 공개할 이미지이므로, 해당 파일에 public read 권한을 추가합니다.
        return s3Client.getUrl(bucket, fileName).toString(); // 업로드를 한 후, 해당 URL을 DB에 저장할 수 있도록 컨트롤러로 URL을 반환합니다.
    }
}