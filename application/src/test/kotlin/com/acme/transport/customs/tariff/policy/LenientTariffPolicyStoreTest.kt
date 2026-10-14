package com.acme.transport.customs.tariff.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientTariffPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientTariffPolicyStore()
        val item = newLenientTariffPolicy("code-982", "label-409")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
