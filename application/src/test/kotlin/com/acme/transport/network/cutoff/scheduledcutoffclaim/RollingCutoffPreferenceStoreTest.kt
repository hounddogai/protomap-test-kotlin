package com.acme.transport.network.cutoff.scheduledcutoffclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingCutoffPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingCutoffPreferenceStore()
        val item = newRollingCutoffPreference("code-509", "name-171")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
