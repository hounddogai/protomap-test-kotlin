package com.acme.transport.safety.coaching.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveCoachingCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveCoachingCycleStore()
        val item = newAdaptiveCoachingCycle("code-645", "name-809")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
