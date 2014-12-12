#include "opencv2/core/core.hpp"
#include "opencv2/objdetect/objdetect.hpp"
#include <string>
#include <vector>
class MultiDetector{
public:
	MultiDetector(const std::string& hogCascadeFilename,const std::string& lbpCascadeFilename, int minFaceSize){
		if(!hogCascade.load(hogCascadeFilename)){
			CV_Error(CV_StsBadArg, "DetectionBasedTracker::DetectionBasedTracker: Cannot load a cascade from the file '"+hogCascadeFilename+"'");
		}
		if(!lbpCascade.load(lbpCascadeFilename)){
			CV_Error(CV_StsBadArg, "DetectionBasedTracker::DetectionBasedTracker: Cannot load a cascade from the file '"+lbpCascadeFilename+"'");
	    }
		mSize= cv::Size(minFaceSize,minFaceSize);
	}
	void multiDetect(const cv::Mat& imageGray,std::vector<cv::Rect> &RectFaces){
		RectFaces.clear();
		std::vector<int> rejectLevels1;
		std::vector<int> rejectLevels2;

		std::vector<double> levelWeights1;
		std::vector<double> levelWeights2;
        std::vector<double> levelWeights;
        std::vector<double> scales;

        std::vector<cv::Rect> tmps1;
        std::vector<cv::Rect> tmps2;
		cv::Size maxSz(0.7*imageGray.rows,0.7*imageGray.rows);
		lbpCascade.detectMultiScale(imageGray, tmps1, rejectLevels1, levelWeights1, 1.1, 15, 0, mSize, maxSz, true);
		hogCascade.detectMultiScale(imageGray, tmps2, rejectLevels2, levelWeights2, 1.1, 5, 0, mSize, maxSz, true);

		for(int i=0; i<tmps1.size();++i){
				cv::Rect tr = tmps1[i];
				double sl = tr.width/32.0;
                RectFaces.push_back( tr );
                levelWeights.push_back((double)levelWeights1[i]);
                scales.push_back(sl);
		}
        for(int i=0; i<tmps2.size();++i){
        		cv::Rect tr = tmps2[i];
        		double sl = tr.width/32.0;
                RectFaces.push_back(tr);
                levelWeights.push_back((double)levelWeights2[i]);
                scales.push_back(sl);
        }
        cv::groupRectangles_meanshift(RectFaces, levelWeights, scales, 0.1, cv::Size(32,32));
        //cv::groupRectangles(RectFaces, 0, 0.2);

	}
	void setmSize(int sz){
		mSize= cv::Size(sz,sz);
	}
	cv::Size mSize;
	cv::CascadeClassifier lbpCascade;
	cv::CascadeClassifier hogCascade;
};
