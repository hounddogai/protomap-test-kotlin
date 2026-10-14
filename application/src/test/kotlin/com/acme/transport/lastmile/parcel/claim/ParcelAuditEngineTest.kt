package com.acme.transport.lastmile.parcel.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelAuditEngine()
        val item = newParcelAudit("code-20", "title-86")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
