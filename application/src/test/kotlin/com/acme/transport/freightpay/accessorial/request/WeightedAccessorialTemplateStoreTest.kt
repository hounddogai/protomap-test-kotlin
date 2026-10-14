package com.acme.transport.freightpay.accessorial.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedAccessorialTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedAccessorialTemplateStore()
        val item = newWeightedAccessorialTemplate("code-505", "name-731")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
