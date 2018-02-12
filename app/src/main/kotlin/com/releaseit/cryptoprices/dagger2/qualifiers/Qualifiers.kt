package com.releaseit.rxrepository.dagger2.qualifiers

import javax.inject.Qualifier

/**
 * Created by jurajbegovac on 27/01/2018.
 */
@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationContext

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityContext
