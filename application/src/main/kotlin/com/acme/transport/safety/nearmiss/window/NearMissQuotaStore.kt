package com.acme.transport.safety.nearmiss.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss quotas by code. */
interface NearMissQuotaStore {
    fun find(code: String): NearMissQuota?
    fun save(item: NearMissQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissQuotaStore(
    var items: MutableMap<String, NearMissQuota>,
    var order: MutableList<String>,
) : NearMissQuotaStore {
    override fun find(code: String): NearMissQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissQuota) {
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

    override fun list(): MutableList<NearMissQuota> {
        val out = mutableListOf<NearMissQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissQuotaStore(): NearMissQuotaStore {
    return MemoryNearMissQuotaStore(mutableMapOf<String, NearMissQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissQuotaStore(
    var inner: NearMissQuotaStore,
    var cache: TtlCache<NearMissQuota>,
    var hits: Int,
    var misses: Int,
) : NearMissQuotaStore {
    override fun find(code: String): NearMissQuota? {
        val hit = (cache.get(code) as NearMissQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissQuota> = inner.list()
}

fun newCachedNearMissQuotaStore(inner: NearMissQuotaStore, ttl: Int, clock: Clock): NearMissQuotaStore {
    return CachedNearMissQuotaStore(
        inner = inner,
        cache = newTtlCache<NearMissQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss quotas. Empty fields match all. */
data class NearMissQuotaQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissQuota): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissQuota>): MutableList<NearMissQuota> {
        val matched = filter(items) { candidate: NearMissQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissQuotas(store: NearMissQuotaStore, query: NearMissQuotaQuery): MutableList<NearMissQuota> {
    return query.apply(store.list())
}
