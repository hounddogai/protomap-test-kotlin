package com.acme.transport.terminal.traileryard.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerYardAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerYardAgreementStore()
        val item = newTrailerYardAgreement("code-385", "title-445")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
