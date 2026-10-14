package com.acme.transport.terminal.crossdock.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockTargetStore()
        val item = newCrossDockTarget("code-614", "name-882")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
