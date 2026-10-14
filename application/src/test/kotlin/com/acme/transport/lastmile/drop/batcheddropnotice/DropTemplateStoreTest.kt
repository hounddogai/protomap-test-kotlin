package com.acme.transport.lastmile.drop.batcheddropnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropTemplateStore()
        val item = newDropTemplate("code-958", "label-878")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
