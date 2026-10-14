package com.acme.transport.lastmile.proofofdelivery.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingProofOfDeliveryPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingProofOfDeliveryPreferenceStore()
        val item = newRollingProofOfDeliveryPreference("code-816", "name-369")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
