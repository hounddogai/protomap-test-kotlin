package com.acme.transport.customs.hold.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveHoldTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveHoldTargetStore()
        val item = newAdaptiveHoldTarget("code-409", "name-70")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
