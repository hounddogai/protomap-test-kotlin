package com.acme.transport.lastmile.parcel.tieredparcelvariance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingParcelVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingParcelVarianceEngine(newRegistry())
        val item = newRollingParcelVariance("code-575", "label-975")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
