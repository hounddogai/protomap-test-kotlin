package com.acme.transport.linehaul.relay.relayquota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps manual relay quotas by code. */
interface ManualRelayQuotaStore {
    fun find(code: String): ManualRelayQuota?
    fun save(item: ManualRelayQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualRelayQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualRelayQuotaStore(
    var items: MutableMap<String, ManualRelayQuota>,
    var order: MutableList<String>,
) : ManualRelayQuotaStore {
    override fun find(code: String): ManualRelayQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualRelayQuota) {
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

    override fun list(): MutableList<ManualRelayQuota> {
        val out = mutableListOf<ManualRelayQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualRelayQuotaStore(): ManualRelayQuotaStore {
    return MemoryManualRelayQuotaStore(mutableMapOf<String, ManualRelayQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualRelayQuotaStore(
    var inner: ManualRelayQuotaStore,
    var cache: TtlCache<ManualRelayQuota>,
    var hits: Int,
    var misses: Int,
) : ManualRelayQuotaStore {
    override fun find(code: String): ManualRelayQuota? {
        val hit = (cache.get(code) as ManualRelayQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualRelayQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualRelayQuota> = inner.list()
}

fun newCachedManualRelayQuotaStore(inner: ManualRelayQuotaStore, ttl: Int, clock: Clock): ManualRelayQuotaStore {
    return CachedManualRelayQuotaStore(
        inner = inner,
        cache = newTtlCache<ManualRelayQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual relay quotas. Empty fields match all. */
data class ManualRelayQuotaQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualRelayQuota): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualRelayQuota>): MutableList<ManualRelayQuota> {
        val matched = filter(items) { candidate: ManualRelayQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualRelayQuotas(store: ManualRelayQuotaStore, query: ManualRelayQuotaQuery): MutableList<ManualRelayQuota> {
    return query.apply(store.list())
}
