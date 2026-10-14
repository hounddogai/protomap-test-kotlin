package com.acme.transport.customs.declaration.declarationforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingDeclarationReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingDeclarationReservationStore()
        val item = newPendingDeclarationReservation("code-799", "label-205")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
