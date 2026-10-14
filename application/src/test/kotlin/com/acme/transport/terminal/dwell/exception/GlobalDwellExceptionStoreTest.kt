package com.acme.transport.terminal.dwell.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalDwellExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalDwellExceptionStore()
        val item = newGlobalDwellException("code-576", "title-456")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
