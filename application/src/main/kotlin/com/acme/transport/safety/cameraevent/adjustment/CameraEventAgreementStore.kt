package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event agreements by code. */
interface CameraEventAgreementStore {
    fun find(code: String): CameraEventAgreement?
    fun save(item: CameraEventAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventAgreementStore(
    var items: MutableMap<String, CameraEventAgreement>,
    var order: MutableList<String>,
) : CameraEventAgreementStore {
    override fun find(code: String): CameraEventAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventAgreement) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<CameraEventAgreement> {
        val out = mutableListOf<CameraEventAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventAgreementStore(): CameraEventAgreementStore {
    return MemoryCameraEventAgreementStore(
        mutableMapOf<String, CameraEventAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventAgreementStore(
    var inner: CameraEventAgreementStore,
    var cache: TtlCache<CameraEventAgreement>,
    var hits: Int,
    var misses: Int,
) : CameraEventAgreementStore {
    override fun find(code: String): CameraEventAgreement? {
        val hit = (cache.get(code) as CameraEventAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventAgreement> = inner.list()
}

fun newCachedCameraEventAgreementStore(inner: CameraEventAgreementStore, ttl: Int, clock: Clock): CameraEventAgreementStore {
    return CachedCameraEventAgreementStore(
        inner = inner,
        cache = newTtlCache<CameraEventAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event agreements. Empty fields match all. */
data class CameraEventAgreementQuery(
    var label: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventAgreement): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventAgreement>): MutableList<CameraEventAgreement> {
        val matched = filter(items) { candidate: CameraEventAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventAgreements(store: CameraEventAgreementStore, query: CameraEventAgreementQuery): MutableList<CameraEventAgreement> {
    return query.apply(store.list())
}
