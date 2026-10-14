package com.acme.transport.freightpay.accessorial.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryAccessorialOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryAccessorialOverrideStore()
        val item = newPrimaryAccessorialOverride("code-11", "name-352")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
