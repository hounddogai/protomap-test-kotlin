package com.acme.transport.terminal.crossdock.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockVarianceStore()
        val item = newCrossDockVariance("code-98", "name-619")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
