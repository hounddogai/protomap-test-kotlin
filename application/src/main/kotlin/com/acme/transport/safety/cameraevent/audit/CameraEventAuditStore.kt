package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event audits by code. */
interface CameraEventAuditStore {
    fun find(code: String): CameraEventAudit?
    fun save(item: CameraEventAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventAuditStore(
    var items: MutableMap<String, CameraEventAudit>,
    var order: MutableList<String>,
) : CameraEventAuditStore {
    override fun find(code: String): CameraEventAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventAudit) {
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

    override fun list(): MutableList<CameraEventAudit> {
        val out = mutableListOf<CameraEventAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventAuditStore(): CameraEventAuditStore {
    return MemoryCameraEventAuditStore(mutableMapOf<String, CameraEventAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventAuditStore(
    var inner: CameraEventAuditStore,
    var cache: TtlCache<CameraEventAudit>,
    var hits: Int,
    var misses: Int,
) : CameraEventAuditStore {
    override fun find(code: String): CameraEventAudit? {
        val hit = (cache.get(code) as CameraEventAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventAudit> = inner.list()
}

fun newCachedCameraEventAuditStore(inner: CameraEventAuditStore, ttl: Int, clock: Clock): CameraEventAuditStore {
    return CachedCameraEventAuditStore(
        inner = inner,
        cache = newTtlCache<CameraEventAudit>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event audits. Empty fields match all. */
data class CameraEventAuditQuery(
    var source: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventAudit): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventAudit>): MutableList<CameraEventAudit> {
        val matched = filter(items) { candidate: CameraEventAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventAudits(store: CameraEventAuditStore, query: CameraEventAuditQuery): MutableList<CameraEventAudit> {
    return query.apply(store.list())
}
