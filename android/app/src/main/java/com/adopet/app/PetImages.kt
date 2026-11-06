package com.adopet.app

// Fotografias ilustrativas para os dados de demonstração.
object PetImages {
    fun resource(animal: Animal): Int {
        return when (animal.species) {
            "Cachorro" -> R.drawable.pet_dog
            "Gato" -> R.drawable.pet_cat
            else -> R.drawable.ic_paw
        }
    }
}
