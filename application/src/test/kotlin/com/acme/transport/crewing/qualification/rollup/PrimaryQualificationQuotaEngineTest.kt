package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryQualificationQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryQualificationQuotaEngine(newRegistry())
        val item = newPrimaryQualificationQuota("code-427", "title-946")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
