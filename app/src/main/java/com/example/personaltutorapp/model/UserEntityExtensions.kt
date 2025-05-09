package com.example.personaltutorapp.model

fun UserEntity.toUser(): User {
    return User(
        id = id,
        email = email,
        password = password,
        displayName = displayName,
        role = role,
        bio = bio,
        profileImageUrl = profileImageUrl
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        password = password,
        displayName = displayName,
        role = role,
        bio = bio,
        profileImageUrl = profileImageUrl
    )
}
