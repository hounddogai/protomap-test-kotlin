package com.acme.transport.customs.declaration.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DeclarationReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDeclarationReservationStore()
        val item = newDeclarationReservation("code-422", "label-321")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
