package com.acme.transport.network.cutoff.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingCutoffVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingCutoffVarianceStore()
        val item = newRollingCutoffVariance("code-889", "name-307")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
