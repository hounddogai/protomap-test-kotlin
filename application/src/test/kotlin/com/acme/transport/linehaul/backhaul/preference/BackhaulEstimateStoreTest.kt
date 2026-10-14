package com.acme.transport.linehaul.backhaul.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulEstimateStore()
        val item = newBackhaulEstimate("code-743", "name-483")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
