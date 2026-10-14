package com.acme.transport.emissions.fuelburn.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingFuelBurnOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingFuelBurnOverrideStore()
        val item = newRollingFuelBurnOverride("code-21", "name-190")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
