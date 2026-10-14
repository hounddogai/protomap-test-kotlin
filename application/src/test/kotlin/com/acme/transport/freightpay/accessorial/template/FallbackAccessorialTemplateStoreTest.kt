package com.acme.transport.freightpay.accessorial.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackAccessorialTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackAccessorialTemplateStore()
        val item = newFallbackAccessorialTemplate("code-278", "label-35")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
