package example.service

import java.util.concurrent.ExecutorService

import example.query.Positions
import example.service.airphrame.service.ModelService
import slick.basic.BasicBackend

class PositionService(implicit
	database: BasicBackend#DatabaseDef,
	executionService: ExecutorService)
	extends ModelService(Positions)