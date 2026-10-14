package com.acme.transport.linehaul.trailer.trailergroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingTrailerGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingTrailerGroupStore()
        val item = newRollingTrailerGroup("code-840", "label-303")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
