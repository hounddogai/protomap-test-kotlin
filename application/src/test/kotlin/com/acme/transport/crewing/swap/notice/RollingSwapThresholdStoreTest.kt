package com.acme.transport.crewing.swap.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingSwapThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingSwapThresholdStore()
        val item = newRollingSwapThreshold("code-954", "title-771")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
