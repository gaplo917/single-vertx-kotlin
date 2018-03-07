package io.vertx.example

import dagger.Module
import dagger.Provides

@Module
class UserModule {

  @Provides
  fun provideUserService(): UserService {
    return UserServiceImpl()
  }

  @Provides
  fun provideUserRepository(): UserRepository {
    return UserRepositoryImpl()
  }
}
