package com.acme.transport.crewing.qualification.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredQualificationLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredQualificationLedgerEngine()
        val item = newTieredQualificationLedger("code-461", "title-748")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
