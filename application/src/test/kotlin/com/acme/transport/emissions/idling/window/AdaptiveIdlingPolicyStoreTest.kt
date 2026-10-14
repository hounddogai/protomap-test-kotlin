package com.acme.transport.emissions.idling.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveIdlingPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveIdlingPolicyStore()
        val item = newAdaptiveIdlingPolicy("code-625", "title-664")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
