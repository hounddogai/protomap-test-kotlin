package com.acme.transport.network.hub.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingHubEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingHubEstimateStore()
        val item = newRollingHubEstimate("code-858", "label-22")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
