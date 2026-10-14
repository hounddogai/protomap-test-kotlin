package com.acme.transport.linehaul.backhaul.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulTemplateStore()
        val item = newBackhaulTemplate("code-306", "name-224")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
