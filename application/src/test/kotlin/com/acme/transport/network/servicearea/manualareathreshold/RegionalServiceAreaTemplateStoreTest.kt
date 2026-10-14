package com.acme.transport.network.servicearea.manualareathreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalServiceAreaTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalServiceAreaTemplateStore()
        val item = newRegionalServiceAreaTemplate("code-732", "label-616")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
