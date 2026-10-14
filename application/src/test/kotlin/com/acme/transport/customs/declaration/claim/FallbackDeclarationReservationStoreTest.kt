package com.acme.transport.customs.declaration.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackDeclarationReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackDeclarationReservationStore()
        val item = newFallbackDeclarationReservation("code-807", "name-202")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
