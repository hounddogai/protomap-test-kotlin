package com.acme.transport.fleetops.utilization.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UtilizationScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryUtilizationScoreStore()
        val item = newUtilizationScore("code-235", "name-619")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
