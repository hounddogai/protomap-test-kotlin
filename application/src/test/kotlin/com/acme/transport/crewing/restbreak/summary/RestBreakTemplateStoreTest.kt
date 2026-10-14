package com.acme.transport.crewing.restbreak.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RestBreakTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRestBreakTemplateStore()
        val item = newRestBreakTemplate("code-32", "name-652")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
