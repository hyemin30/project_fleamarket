package hanghae.fleamarket.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import hanghae.fleamarket.config.ConfigUtils;
import hanghae.fleamarket.dto.GoogleLoginDto;
import hanghae.fleamarket.dto.GoogleLoginRequest;
import hanghae.fleamarket.dto.GoogleLoginResponse;
import hanghae.fleamarket.entity.User;
import hanghae.fleamarket.entity.UserRoleEnum;
import hanghae.fleamarket.jwt.JwtUtil;
import hanghae.fleamarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleService {

    private final ConfigUtils configUtils;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
//    public ResponseEntity<Object> moveGoogleInitUrl() {
//
//        String authUrl = configUtils.googleInitUrl();
//        System.out.println("authUrl: " + authUrl);
//        URI redirectUri = null;
//        try {
//            redirectUri = new URI(authUrl);
//            HttpHeaders httpHeaders = new HttpHeaders();
//            httpHeaders.setLocation(redirectUri);
//            return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
//        } catch (
//                URISyntaxException e) {
//            e.printStackTrace();
//            return ResponseEntity.badRequest().build();
//        }
//    }

    public String redirectGoogleLogin(String authCode){
        // HTTP ????????? ?????? RestTemplate ??????
        RestTemplate restTemplate = new RestTemplate();
        GoogleLoginRequest requestParams = GoogleLoginRequest.builder()
                .clientId(configUtils.getGoogleClientId())
                .clientSecret(configUtils.getGoogleSecret())
                .code(authCode)
                .redirectUri(configUtils.getGoogleRedirectUri())
                .grantType("authorization_code")
                .build();

        try {
            // Http Header ??????
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<GoogleLoginRequest> httpRequestEntity = new HttpEntity<>(requestParams, headers);
            ResponseEntity<String> apiResponseJson = restTemplate.postForEntity(configUtils.getGoogleAuthUrl() + "/token", httpRequestEntity, String.class);

            // ObjectMapper??? ?????? String to Object??? ??????
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // NULL??? ?????? ?????? ????????????(NULL??? ????????? ??????)
            GoogleLoginResponse googleLoginResponse = objectMapper.readValue(apiResponseJson.getBody(), new TypeReference<GoogleLoginResponse>() {});

            // ???????????? ????????? JWT Token?????? ???????????? ??????, Id_Token??? ?????? ????????????.
            String jwtToken = googleLoginResponse.getIdToken();

            // JWT Token??? ????????? JWT ????????? ????????? ?????? ??????
            String requestUrl = UriComponentsBuilder.fromHttpUrl(configUtils.getGoogleAuthUrl() + "/tokeninfo").queryParam("id_token", jwtToken).toUriString();

            String resultJson = restTemplate.getForObject(requestUrl, String.class);

            if(resultJson != null) {
                GoogleLoginDto userInfoDto = objectMapper.readValue(resultJson, new TypeReference<GoogleLoginDto>() {});
                //????????? ????????? ????????????
                String password = UUID.randomUUID().toString();
                String encodedPassword = passwordEncoder.encode(password);
                //?????? ?????? ????????????
                String googleEmail = userInfoDto.getEmail();
                User sameEmailUser = userRepository.findByEmail(googleEmail).orElse(null);
                //????????? ????????? ???????????? ??????
                if (sameEmailUser == null) {
                    userRepository.save(new User(userInfoDto.getName(), encodedPassword, userInfoDto.getEmail(), UserRoleEnum.USER)); //??????????????? USER
                }
                //?????? ?????????
                String createToken =  jwtUtil.createToken(userInfoDto.getName(), UserRoleEnum.USER);

                return createToken;
            }
            else {
                throw new Exception("Google OAuth failed!");
            }
        }
        catch (Exception e) {
            return null;
        }

    }

}
