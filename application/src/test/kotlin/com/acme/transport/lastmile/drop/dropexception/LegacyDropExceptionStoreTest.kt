package com.acme.transport.lastmile.drop.dropexception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyDropExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyDropExceptionStore()
        val item = newLegacyDropException("code-666", "label-989")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
