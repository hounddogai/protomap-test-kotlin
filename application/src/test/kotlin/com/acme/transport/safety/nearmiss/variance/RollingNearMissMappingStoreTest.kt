package com.acme.transport.safety.nearmiss.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingNearMissMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingNearMissMappingStore()
        val item = newRollingNearMissMapping("code-471", "label-564")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
