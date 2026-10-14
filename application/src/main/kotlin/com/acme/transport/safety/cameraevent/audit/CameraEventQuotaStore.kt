package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event quotas by code. */
interface CameraEventQuotaStore {
    fun find(code: String): CameraEventQuota?
    fun save(item: CameraEventQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventQuotaStore(
    var items: MutableMap<String, CameraEventQuota>,
    var order: MutableList<String>,
) : CameraEventQuotaStore {
    override fun find(code: String): CameraEventQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventQuota) {
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

    override fun list(): MutableList<CameraEventQuota> {
        val out = mutableListOf<CameraEventQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventQuotaStore(): CameraEventQuotaStore {
    return MemoryCameraEventQuotaStore(mutableMapOf<String, CameraEventQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventQuotaStore(
    var inner: CameraEventQuotaStore,
    var cache: TtlCache<CameraEventQuota>,
    var hits: Int,
    var misses: Int,
) : CameraEventQuotaStore {
    override fun find(code: String): CameraEventQuota? {
        val hit = (cache.get(code) as CameraEventQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventQuota> = inner.list()
}

fun newCachedCameraEventQuotaStore(inner: CameraEventQuotaStore, ttl: Int, clock: Clock): CameraEventQuotaStore {
    return CachedCameraEventQuotaStore(
        inner = inner,
        cache = newTtlCache<CameraEventQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event quotas. Empty fields match all. */
data class CameraEventQuotaQuery(
    var source: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventQuota): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventQuota>): MutableList<CameraEventQuota> {
        val matched = filter(items) { candidate: CameraEventQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventQuotas(store: CameraEventQuotaStore, query: CameraEventQuotaQuery): MutableList<CameraEventQuota> {
    return query.apply(store.list())
}
