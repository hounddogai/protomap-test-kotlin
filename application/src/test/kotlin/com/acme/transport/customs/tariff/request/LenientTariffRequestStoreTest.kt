package com.acme.transport.customs.tariff.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientTariffRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientTariffRequestStore()
        val item = newLenientTariffRequest("code-478", "name-645")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
