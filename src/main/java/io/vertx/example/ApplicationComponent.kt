package io.vertx.example

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [UserModule::class, RouterModule::class])
interface ApplicationComponent {
  fun inject(main: MainApp)
  fun inject(userController: UserController)
}
