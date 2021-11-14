/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.i18n.icu

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.MessageSourceResolvable
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder

import yakworks.i18n.MsgService

/**
 * Helper class for easy access to messages from a MessageSource,
 * Works with ICU providing various overloaded getMessage methods.
 *
 * loosely based on and similiar to {@link org.springframework.context.support.MessageSourceAccessor}
 * but leans on args being a list and convert to array since its way easier to deal with in groovy
 *
 * has other helpers related to creating Results
 *
 * @since 6.1.11-v6
 */
@SuppressWarnings('AssignmentToStaticFieldFromInstanceMethod')
@CompileStatic
class DefaultMsgService implements ApplicationContextAware {

    public static ApplicationContext appCtx

    @Autowired MsgService messageSource

    //used mostly for testing, not set in production so LocaleContextHolder gets used
    Locale defaultLocale

    void setApplicationContext(ApplicationContext context){
        appCtx = context
    }

    /**
     * Return the default locale to use if no explicit locale has been given.
     * <p>The default implementation returns the default locale passed into the
     * corresponding constructor, or LocaleContextHolder's locale as fallback.
     * Can be overridden in subclasses.
     * @see org.springframework.context.i18n.LocaleContextHolder#getLocale()
     */
    Locale getDefaultLocale() {
        return (this.defaultLocale != null ? this.defaultLocale : LocaleContextHolder.getLocale());
    }

    /**
     * Retrieve the message for the given code and the default Locale.
     * @param code code of the message
     * @param defaultMessage String to return if the lookup fails
     * @return the message
     */
    // String getMessage(String code, String defaultMessage) {
    //     return messageSource.getMessage(code, [], defaultMessage, getDefaultLocale());
    // }


    /**
     * Retrieve the message for the given code and the default Locale.
     * @param code code of the message
     * @param args arguments for the message, or {@code null} if none
     * @param defaultMessage String to return if the lookup fails
     * @return the message
     */
    // String getMessage(String code, List args, String defaultMessage) {
    //     return messageSource.getMessage(code, args as Object[], defaultMessage, getDefaultLocale());
    // }

    /**
     * Retrieve the message for the given code and the default Locale.
     * @param code code of the message
     * @return the message
     * @throws org.springframework.context.NoSuchMessageException if not found
     */
    // String getMessage(String code) throws NoSuchMessageException {
    //     return messageSource.getMessage(code, getDefaultLocale());
    // }

    /**
     * Retrieve the message for the given code and the default Locale.
     * @param code code of the message
     * @param args arguments for the message, or {@code null} if none
     * @return the message
     * @throws org.springframework.context.NoSuchMessageException if not found
     */
    // String getMessage(String code, List args) throws NoSuchMessageException {
    //     return messageSource.getMessage(code, args as Object[], getDefaultLocale());
    // }

    /**
     * Retrieve the given MessageSourceResolvable (e.g. an ObjectError instance)
     * in the default Locale.
     * @param resolvable the MessageSourceResolvable
     * @return the message
     * @throws org.springframework.context.NoSuchMessageException if not found
     */
    // String getMessage(MessageSourceResolvable resolvable) throws NoSuchMessageException {
    //     return messageSource.getMessage(resolvable, getDefaultLocale());
    // }

    /**
     * If no message found then this one swallows the NoSuchMessageException
     * and returns an empty string
     */
    // String getMessageSafe(MessageSourceResolvable resolvable){
    //     try {
    //         getMessage(resolvable)
    //     }catch(NoSuchMessageException e){
    //         return ''
    //     }
    // }

    /**
     * AVOID, static cheater if in static context where we can't inject service
     */
    static DefaultMsgService get() {
        appCtx.getBean('msgService', this)
    }

    /**
     * AVOID, static cheater if in context where can't inject service
     */
    // static String get(MessageSourceResolvable msr) {
    //     DefaultMsgService.get().getMessage(msr)
    // }
}