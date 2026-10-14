package com.acme.transport.network.cutoff.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientCutoffTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientCutoffTemplateStore()
        val item = newLenientCutoffTemplate("code-492", "title-113")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
