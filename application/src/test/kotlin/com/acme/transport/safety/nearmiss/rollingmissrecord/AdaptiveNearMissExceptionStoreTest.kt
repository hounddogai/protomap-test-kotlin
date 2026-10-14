package com.acme.transport.safety.nearmiss.rollingmissrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveNearMissExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveNearMissExceptionStore()
        val item = newAdaptiveNearMissException("code-210", "name-917")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
