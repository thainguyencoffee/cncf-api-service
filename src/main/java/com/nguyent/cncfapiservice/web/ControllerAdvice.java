package com.nguyent.cncfapiservice.web;

import com.nguyent.cncfapiservice.cloudinary.CloudinaryErrorException;
import com.nguyent.cncfapiservice.domain.comment.CommentNotFoundException;
import com.nguyent.cncfapiservice.domain.comment.DeleteCommentFailureException;
import com.nguyent.cncfapiservice.domain.interact.DeleteInteractFailureException;
import com.nguyent.cncfapiservice.domain.interact.InteractEmptyException;
import com.nguyent.cncfapiservice.domain.interact.InteractNotFoundException;
import com.nguyent.cncfapiservice.domain.post.PostNotFoundException;
import com.nguyent.cncfapiservice.domain.post.PostsEmptyException;
import com.nguyent.cncfapiservice.domain.user.UserRepresentationNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@RestControllerAdvice
public class ControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(ControllerAdvice.class);

    @ExceptionHandler(UserRepresentationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseApi handleUserRepresentationNotFoundException(UserRepresentationNotFoundException ex) {
        return new ResponseApi("Not Found", 404, ex.getLocalizedMessage(), null, null);
    }

    @ExceptionHandler(PostNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseApi handleUserRepresentationNotFoundException(PostNotFoundException ex) {
        return new ResponseApi("Not Found", 404, ex.getLocalizedMessage(), null, null);
    }

    @ExceptionHandler(PostsEmptyException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi handlePostsEmptyException(PostsEmptyException ex) {
        return new ResponseApi("OK", 200, ex.getLocalizedMessage(), null, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseApi handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return new ResponseApi("Bad request", 400, ex.getLocalizedMessage(), null, null);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseApi handleRuntimeException(RuntimeException ex) {
        log.error(ex.getLocalizedMessage());
        return new ResponseApi("Internal Server Error", 500, ex.getMessage(), null, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseApi handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        var error = new HashMap<String, String>();
        ResponseApi responseApi = new ResponseApi("Bad request", 400, ex.getLocalizedMessage(), error, null);
        ex.getBindingResult().getAllErrors().forEach(objectError -> {
            String message = objectError.getObjectName() + " object has errors.";
            String fieldError = ((FieldError) objectError).getField();
            String errorMessage = objectError.getDefaultMessage();
            error.put(fieldError, errorMessage);
            responseApi.setMessage(message);
        });
        return responseApi;
    }

    @ExceptionHandler(InteractEmptyException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseApi handleInteractEmptyException(InteractEmptyException ex) {
        return new ResponseApi("Not Found", 404,  ex.getLocalizedMessage(), null, null);
    }

    @ExceptionHandler(DeleteInteractFailureException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseApi handleDeleteInteractFailureException(DeleteInteractFailureException ex) {
        return new ResponseApi("Bad request", 400, ex.getLocalizedMessage(), null, null);
    }

    @ExceptionHandler(InteractNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseApi handleInteractNotFoundException(InteractNotFoundException ex) {
        return new ResponseApi("Not Found", 404, ex.getLocalizedMessage(), null, null);
    }

    @ExceptionHandler(DeleteCommentFailureException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseApi handleDeleteCommentFailureException(DeleteCommentFailureException ex) {
        return new ResponseApi("Bad request", 400, ex.getLocalizedMessage(), null, null);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseApi handleCommentNotFoundException(CommentNotFoundException ex) {
        return new ResponseApi("Not Found", 404, ex.getLocalizedMessage(), null, null);
    }

    @ExceptionHandler(CloudinaryErrorException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseApi handleCloudinaryErrorException(CloudinaryErrorException ex) {
        return new ResponseApi("Unprocessable Entity", 422, ex.getLocalizedMessage(), null, null);
    }

}
